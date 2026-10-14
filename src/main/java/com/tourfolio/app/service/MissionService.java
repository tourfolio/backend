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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final AttendanceService attendanceService;
    private final NotificationService notificationService;

    // 수집 미션: 카드 보유 판정은 프론트가 하고, 서버는 claim 호출만 믿고 처리한다
    private static final String COLLECT_CONDITION_TYPE = "CARD_COUNT";

    /**
     * 업적 화면 조회. 진행도만 계산하며 포인트 지급/완료 처리/알림은 하지 않는다.
     * isCompleted = 보상을 수령(claim)한 상태
     * 수령 가능 여부는 currentProgress >= conditionTarget && !isCompleted 로 판단한다.
     */
    @Transactional
    public MissionListResponse getMissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        List<Mission> allMissions = missionRepository.findAll();

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

            boolean claimed = Boolean.TRUE.equals(um.getIsCompleted());
            int target = mission.getConditionTarget();
            int progress;

            if (claimed) {
                progress = target;
            } else if (COLLECT_CONDITION_TYPE.equals(mission.getConditionType())) {
                // 수집 미션의 미완료 진행도는 프론트가 계산하므로 서버는 0
                progress = 0;
            } else {
                int live = switch (mission.getConditionType()) {
                    case "FIRST_BUY" -> hasFirstBuy ? 1 : 0;
                    case "STOCK_QTY_SINGLE" -> maxSingleQty;
                    case "STOCK_QTY_DISTINCT" -> distinctStockCount;
                    case "CONSECUTIVE_DAYS" -> consecutiveDays;
                    case "CUMULATIVE_DAYS" -> cumulativeDays;
                    default -> 0;
                };
                int stored = um.getCurrentProgress() != null ? um.getCurrentProgress() : 0;
                // 한 번 달성한 진행도는 유지 (출석이 끊기거나 주식을 팔아도 수령 가능 상태가 사라지지 않게)
                progress = Math.min(Math.max(stored, live), target);
            }

            um.setCurrentProgress(progress);
            userMissionRepository.save(um);

            if (claimed) completed++;
            else inProgress++;

            responses.add(MissionResponse.builder()
                    .missionId(mission.getId())
                    .category(mission.getCategory())
                    .title(mission.getTitle())
                    .rewardPoints(mission.getRewardPoints())
                    .currentProgress(progress)
                    .conditionTarget(target)
                    .isCompleted(claimed)
                    .build());
        }

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

    /**
     * 미션 보상 수령 (모든 미션 공통). 완료 처리 + 포인트 지급 + MISSION_COMPLETE 알림을 한 트랜잭션으로 처리한다.
     * 동시 요청/재요청이 와도 보상은 한 번만 지급된다.
     */
    @Transactional
    public MissionClaimResponse claimMission(Long userId, Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new CustomException("MISSION_NOT_FOUND", "존재하지 않는 미션입니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        UserMission existing = userMissionRepository.findByUserIdAndMissionId(userId, missionId).orElse(null);

        // 이미 수령한 경우 (재요청)
        if (existing != null && Boolean.TRUE.equals(existing.getIsCompleted())) {
            return MissionClaimResponse.builder()
                    .missionId(missionId)
                    .isCompleted(true)
                    .alreadyRewarded(true)
                    .pointsAwarded(0)
                    .balance(user.getBalance())
                    .build();
        }

        // 수집 미션 외에는 서버가 달성 여부를 직접 검증
        if (!COLLECT_CONDITION_TYPE.equals(mission.getConditionType())) {
            int stored = (existing != null && existing.getCurrentProgress() != null) ? existing.getCurrentProgress() : 0;
            int live = calculateLiveProgress(mission.getConditionType(), userId);
            if (Math.max(stored, live) < mission.getConditionTarget()) {
                throw new CustomException("MISSION_NOT_ACHIEVED", "아직 달성하지 못한 미션입니다.");
            }
        }

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

        log.info("미션 보상 수령: userId={}, missionId={}, +{}P", userId, missionId, mission.getRewardPoints());

        return MissionClaimResponse.builder()
                .missionId(missionId)
                .isCompleted(true)
                .alreadyRewarded(false)
                .pointsAwarded(mission.getRewardPoints())
                .balance(user.getBalance())
                .build();
    }

    // claim 시점의 실시간 진행도 계산 (수집 미션 제외)
    private int calculateLiveProgress(String conditionType, Long userId) {
        return switch (conditionType) {
            case "FIRST_BUY" -> transactionRepository.findByMemberIdOrderByExecutedAtDesc(userId).stream()
                    .anyMatch(t -> "BUY".equals(t.getType())) ? 1 : 0;
            case "STOCK_QTY_SINGLE" -> portfolioRepository.findByMemberId(userId).stream()
                    .mapToInt(p -> p.getQuantity().intValue())
                    .max().orElse(0);
            case "STOCK_QTY_DISTINCT" -> (int) portfolioRepository.findByMemberId(userId).stream()
                    .filter(p -> p.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                    .count();
            case "CONSECUTIVE_DAYS" -> attendanceService.calculateConsecutiveDays(userId);
            case "CUMULATIVE_DAYS" -> attendanceService.getTotalAttendanceCount(userId);
            default -> 0;
        };
    }
}