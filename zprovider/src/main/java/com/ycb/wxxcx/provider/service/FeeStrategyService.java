package com.ycb.wxxcx.provider.service;

import com.ycb.wxxcx.provider.mapper.FeeStrategyMapper;
import com.ycb.wxxcx.provider.mapper.ShopStationMapper;
import com.ycb.wxxcx.provider.utils.TimeUtil;
import com.ycb.wxxcx.provider.vo.FeeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Created by zhuhui on 17-8-28.
 */
@Service
public class FeeStrategyService {

    public static final Logger logger = LoggerFactory.getLogger(FeeStrategyService.class);

    @Autowired
    private ShopStationMapper shopStationMapper;

    @Autowired
    private FeeStrategyMapper feeStrategyMapper;

    public String transFeeStrategy(FeeStrategy feeStrategy) {
        //固定收费
        Long fixedTime = feeStrategy.getFixedTime();
        Long fixedUnit = feeStrategy.getFixedUnit();
        BigDecimal fixed = feeStrategy.getFixed();
        String fixedStr = StringUtils.isEmpty(fixed) ? "" : "每" + fixedTime + TimeUtil.getUnitString(fixedUnit) + "收费" + fixed + "元。";

        //超出后每小时收费1元。
        BigDecimal fee = feeStrategy.getFee();
        Long feeUnit = feeStrategy.getFeeUnit();
        String feeStr = StringUtils.isEmpty(fee) ? "" : "超出后每" + TimeUtil.getUnitString(feeUnit) + "收费" + fee + "元。";

        // 每天最高收费
        Long maxFeeUnit = feeStrategy.getMaxFeeUnit();
        Long maxFeeTime = feeStrategy.getMaxFeeTime();
        BigDecimal maxFee = feeStrategy.getMaxFee();
        String maxFeeUnitStr = TimeUtil.getUnitString(maxFeeUnit);
        String maxFeeStr = StringUtils.isEmpty(maxFee) ? "" : "每" + maxFeeTime + maxFeeUnitStr + "最高收费" + maxFee + "元。";
        return fixedStr + feeStr + maxFeeStr;
    }

    /**
     * 根据收费策略计算已花费租金
     *
     * @param feeStrategy
     * @param duration
     * @param status      已完成订单直接返回费用
     * @param usedFee     已完成订单费用
     * @return
     */
    public BigDecimal calUseFee(FeeStrategy feeStrategy, Long duration, Integer status, BigDecimal usedFee) {
        BigDecimal useFee = BigDecimal.ZERO;
        Long freeTimeUnit = feeStrategy.getFreeTime() * feeStrategy.getFreeUnit();
        Long maxFeeTimeUnit = feeStrategy.getMaxFeeTime() * feeStrategy.getMaxFeeUnit();
        if (2 != status) {
            return usedFee;
        }
        if (duration == null || duration < freeTimeUnit) {
            // 如果租借时长小于意外借出时间，则不计费用
            return useFee;
        } else if (duration > maxFeeTimeUnit) {
            // 租借时长大于最高收费时长，按最高收费
            // 共借出总天数
            Integer days = Math.toIntExact(duration / Long.valueOf(24 * 60 * 60));
            if (days.equals(0)) {
                useFee = feeStrategy.getMaxFee();
            } else {
                BigDecimal todayUsedFee = this.calUseFee(feeStrategy, duration - Long.valueOf(24 * 60 * 60), status, usedFee);
                useFee = feeStrategy.getMaxFee().multiply(BigDecimal.valueOf(days)).add(todayUsedFee);
            }
        } else {
            // 超出时间
            Long expirTime = duration - feeStrategy.getFixedTime() * feeStrategy.getFixedUnit();
            if (expirTime < 0) {
                useFee = feeStrategy.getFixed();
            } else {
                useFee = feeStrategy.getFixed().add(feeStrategy.getFee().multiply(BigDecimal.valueOf((expirTime / feeStrategy.getFeeUnit()))));
            }
        }
        return useFee;
    }

    public FeeStrategy findFeeStrategyByStation(Long stationid) {
        FeeStrategy fFeeStrategy = shopStationMapper.findFeeStrategyByStation(stationid);
        if (fFeeStrategy == null) {
            // 获取全局收费配置
            fFeeStrategy = feeStrategyMapper.findGlobalFeeStrategy();
        }
        return fFeeStrategy;
    }

    public BigDecimal calNeedPay(BigDecimal defaultPay, BigDecimal usablemoney) {
        if (defaultPay.compareTo(usablemoney) < 0) {
            return BigDecimal.ZERO;
        } else {
            return defaultPay.subtract(usablemoney);
        }
    }
}
