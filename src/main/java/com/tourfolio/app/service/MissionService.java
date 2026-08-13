package com.tourfolio.app.service;

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
    private final UserCardRepository userCardRepository;
    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final AttendanceService attendanceService;

    @Transactional
    public MissionListResponse getMissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        List<Mission> allMissions = missionRepository.findAll();

        // 진행률 계산에 필요한 값들 미리 조회
        int cardCount = userCardRepository.countByUserId(userId).intValue();

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
        int cumulativeDays = attendanceService.getTotalAttendanceCount(userId); // 아래 루프에서 attendanceRepository 대신 재사용 위해 여기선 계산 안 함 - 별도 조회 필요시 AttendanceRepository 주입

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

            int progress = switch (mission.getConditionType()) {
                case "CARD_COUNT" -> cardCount;
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

        List<Boolean> weekly = attendanceService.calculateWeeklyAttendance(userId);
        boolean attendedToday = weekly.get(LocalDate.now().getDayOfWeek().getValue() - 1);

        return MissionListResponse.builder()
                .balance(user.getBalance())
                .weeklyAttendance(weekly)
                .attendedToday(attendedToday)
                .inProgressCount(inProgress)
                .completedCount(completed)
                .missions(responses)
                .build();
    }
}