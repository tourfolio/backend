// src/main/java/com/tourfolio/app/service/MissionService.java
package com.tourfolio.app.service;

import com.tourfolio.app.dto.MissionClaimResponse;
import com.tourfolio.app.dto.MissionListResponse;
import com.tourfolio.app.dto.MissionResponse;
import com.tourfolio.app.entity.*;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final UserRepository userRepository;
    private final UserCardRepository userCardRepository;
    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final AttendanceService attendanceService;
    private final NotificationService notificationService;

    // 프론트 판정으로 완료 처리 가능한 COLLECT 미션만 허용 (첫 발도장 / 길 위의 사람 / 대한민국 정복)
    private static final Set<Long> COLLECT_MISSION_IDS = Set.of(1L, 2L, 3L);

    @Transactional
    public MissionListResponse getMissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        List<Mission> allMissions = missionRepository.findAll();

        // 진행률 계산에 필요한 값들 미리 조회 (CARD_COUNT는 이제 프론트 판정 + claim API로만 완료 처리)
        boolean hasFirstBuy = transactionRepository.findByMemberIdOrderByExecutedAtDesc(userId).stream()
                .anyMatch(t -> "BUY".equals(t.getType()));

        List<Portfolio> portfolios = portfolioRepository.findByMemberId(userId);
        int maxSingleQty = portfolios.stream()
                .mapToInt(p -> p.getQuantity().intValue())
                .max().orElse(0);
        int distinctStockCount = (int) portfolios.stream()
                .filter(p -> p.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .count();

        int consecutiveDays = attendanceService.calculateConsecutiveDays(userId);
        int cumulativeDays = attendanceService.getTotalAttendanceCount(userId);

        List<MissionResponse> responses = new ArrayList<>();
        int inProgress = 0, completed = 0;

        for (Mission mission : allMissions) {
            UserMission um = userMissionRepository.findByUserIdAndMissionId(userId, mission.getId())
                    .orElse(UserMission.builder()
                            .userId(userId)
                            .missionId(mission.getId())
                            .currentProgress(0)
                            .isCompleted(false)
                            .createdAt(LocalDateTime.now())
                            .build());

            boolean isCollectMission = "CARD_COUNT".equals(mission.getConditionType());

            if (isCollectMission) {
                // COLLECT 미션: 완료 여부는 서버(claim API로만 갱신)가 갖고 있는 값을 그대로 사용.
                // 미완료 상태의 진행도는 프론트에서 계산하므로 서버는 항상 0으로 내려준다.
                if (!Boolean.TRUE.equals(um.getIsCompleted())) {
                    um.setCurrentProgress(0);
                }
                userMissionRepository.save(um);

                if (Boolean.TRUE.equals(um.getIsCompleted())) completed++;
                else inProgress++;

                responses.add(MissionResponse.builder()
                        .missionId(mission.getId())
                        .category(mission.getCategory())
                        .title(mission.getTitle())
                        .rewardPoints(mission.getRewardPoints())
                        .currentProgress(um.getCurrentProgress())
                        .conditionTarget(mission.getConditionTarget())
                        .isCompleted(um.getIsCompleted())
                        .build());
                continue;
            }

            int progress = switch (mission.getConditionType()) {
                case "FIRST_BUY" -> hasFirstBuy ? 1 : 0;
                case "STOCK_QTY_SINGLE" -> maxSingleQty;
                case "STOCK_QTY_DISTINCT" -> distinctStockCount;
                case "CONSECUTIVE_DAYS" -> consecutiveDays;
                case "CUMULATIVE_DAYS" -> cumulativeDays;
                default -> 0;
            };
            progress = Math.min(progress, mission.getConditionTarget());
            um.setCurrentProgress(progress);

            boolean justCompleted = !Boolean.TRUE.equals(um.getIsCompleted()) && progress >= mission.getConditionTarget();
            if (justCompleted) {
                um.setIsCompleted(true);
                um.setCompletedAt(LocalDateTime.now());

                user.setBalance(user.getBalance().add(BigDecimal.valueOf(mission.getRewardPoints())));
                pointHistoryRepository.save(PointHistory.builder()
                        .userId(userId)
                        .type("MISSION")
                        .title(mission.getTitle() + " 미션 달성")
                        .amount((long) mission.getRewardPoints())
                        .createdAt(LocalDateTime.now())
                        .build());

                notificationService.notify(userId, "MISSION_COMPLETE",
                        mission.getTitle() + " 미션을 달성하여 " + mission.getRewardPoints() + "P를 획득했습니다!");

                log.info("미션 달성: userId={}, mission={}, +{}P", userId, mission.getTitle(), mission.getRewardPoints());
            }
            userMissionRepository.save(um);

            if (Boolean.TRUE.equals(um.getIsCompleted())) completed++;
            else inProgress++;

            responses.add(MissionResponse.builder()
                    .missionId(mission.getId())
                    .category(mission.getCategory())
                    .title(mission.getTitle())
                    .rewardPoints(mission.getRewardPoints())
                    .currentProgress(um.getCurrentProgress())
                    .conditionTarget(mission.getConditionTarget())
                    .isCompleted(um.getIsCompleted())
                    .build());
        }

        userRepository.save(user);

        List<String> weekly = attendanceService.calculateWeeklyAttendance(userId);
        boolean attendedToday = "ATTENDED".equals(weekly.get(LocalDate.now().getDayOfWeek().getValue() - 1));

        return MissionListResponse.builder()
                .balance(user.getBalance())
                .weeklyAttendance(weekly)
                .attendedToday(attendedToday)
                .inProgressCount(inProgress)
                .completedCount(completed)
                .missions(responses)
                .build();
    }

    @Transactional
    public MissionClaimResponse claimCollectMission(Long userId, Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new CustomException("MISSION_NOT_FOUND", "존재하지 않는 미션입니다."));

        if (!COLLECT_MISSION_IDS.contains(missionId)) {
            throw new CustomException("MISSION_NOT_CLAIMABLE", "프론트 판정으로 완료 처리할 수 없는 미션입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        // 이미 완료된 상태인지 먼저 확인 (재요청 케이스)
        UserMission existing = userMissionRepository.findByUserIdAndMissionId(userId, missionId).orElse(null);
        if (existing != null && Boolean.TRUE.equals(existing.getIsCompleted())) {
            return MissionClaimResponse.builder()
                    .missionId(missionId)
                    .isCompleted(true)
                    .alreadyRewarded(true)
                    .pointsAwarded(0)
                    .balance(user.getBalance())
                    .build();
        }

        // 레코드가 아예 없으면 먼저 생성 (완료 전 상태로)
        if (existing == null) {
            userMissionRepository.save(UserMission.builder()
                    .userId(userId)
                    .missionId(missionId)
                    .currentProgress(0)
                    .isCompleted(false)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        // 완료 안 된 것만 조건부로 완료 처리 (동시 요청 시 단 하나만 성공)
        int updated = userMissionRepository.markCompletedIfNotAlready(userId, missionId, mission.getConditionTarget());

        if (updated == 0) {
            // 방금 사이에 다른 요청이 먼저 완료 처리한 경우
            User refreshed = userRepository.findById(userId).orElseThrow();
            return MissionClaimResponse.builder()
                    .missionId(missionId)
                    .isCompleted(true)
                    .alreadyRewarded(true)
                    .pointsAwarded(0)
                    .balance(refreshed.getBalance())
                    .build();
        }

        // 여기 도달한 요청만 최초 지급자
        user.setBalance(user.getBalance().add(BigDecimal.valueOf(mission.getRewardPoints())));
        userRepository.save(user);

        pointHistoryRepository.save(PointHistory.builder()
                .userId(userId)
                .type("MISSION")
                .title(mission.getTitle() + " 미션 달성")
                .amount((long) mission.getRewardPoints())
                .createdAt(LocalDateTime.now())
                .build());

        notificationService.notify(userId, "MISSION_COMPLETE",
                mission.getTitle() + " 미션을 달성하여 " + mission.getRewardPoints() + "P를 획득했습니다!");

        log.info("수집 미션 보상 지급: userId={}, missionId={}, +{}P", userId, missionId, mission.getRewardPoints());

        return MissionClaimResponse.builder()
                .missionId(missionId)
                .isCompleted(true)
                .alreadyRewarded(false)
                .pointsAwarded(mission.getRewardPoints())
                .balance(user.getBalance())
                .build();
    }
}