package io.github.nattocb.treasure_seas.core.utility;

import io.github.nattocb.treasure_seas.TreasureSeas;

import java.util.Random;

public class MathUtils {

    /**
     * 输入一个区间和比例值，返回随机子区间
     *
     * @param interval          母区间
     * @param minimumProportion 子区间占比母区间的最小比例
     * @return 子区间
     */
    public static double[] getRandomSubInterval(double[] interval, double minimumProportion) {
        if (interval == null || interval.length != 2 || interval[0] >= interval[1]) {
            TreasureSeas.getLogger().error("MathUtils.getSubInterval: invalid interval: {}", interval);
            return interval;
        }
        Random random = TreasureSeas.RANDOM;
        double intervalLength = interval[1] - interval[0];
        double minSubIntervalLength = intervalLength * minimumProportion;

        // Calculate maximum starting point for the sub-interval
        double maxStart = interval[1] - minSubIntervalLength;
        double subIntervalStart = interval[0] + random.nextDouble() * (maxStart - interval[0]);

        // Calculate random sub-interval length which is not less than minSubIntervalLength
        double maxPossibleLength = interval[1] - subIntervalStart;
        double subIntervalLength = minSubIntervalLength + random.nextDouble() * (maxPossibleLength - minSubIntervalLength);

        // Calculate sub-interval end
        double subIntervalEnd = subIntervalStart + subIntervalLength;

        return new double[]{subIntervalStart, subIntervalEnd};
    }

    /**
     * @param minLength        β概率分布内可能得最低值 cm, e.g. 5
     * @param maxLength        β概率分部内可能得最高值 cm, e.g. 180
     * @param mostCommonLength β概率分部内最高概率的值 cm, e.g. 31
     * @param dispersion       β概率分布的离散程度，一般 [10,0), 调整这个值以控制分布的平稳程度，k越高波峰越高，两端概率越低
     * @return [length, CDF]
     */
    public static double randomFishLength(double minLength, double maxLength, double mostCommonLength, double dispersion) {

        // 标准化模式到 [0, 1] 区间
        double modeNormalized = (mostCommonLength - minLength) / (maxLength - minLength);

        // 使用典型 β 分布参数并调整
        double a = modeNormalized * (dispersion - 2) + 1;
        double b = (1 - modeNormalized) * (dispersion - 2) + 1;

        // 生成两个独立的 γ 随机变量
        Random random = TreasureSeas.RANDOM;
        double ga = generateGamma(a, 1, random);
        double gb = generateGamma(b, 1, random);

        // 返回 β 分布随机变量
        double randomValue = ga / (ga + gb);

        // 缩放到指定范围
        return randomValue * (maxLength - minLength) + minLength;
    }

    /**
     * 生成γ随机变量
     *
     * @param shape
     * @param scale
     * @param random
     * @return
     */
    private static double generateGamma(double shape, double scale, Random random) {
        if (shape < 1) {
            shape += 1;
            double u = random.nextDouble();
            return generateGamma(shape, scale, random) * Math.pow(u, 1 / shape);
        }

        double d = shape - 1.0 / 3.0;
        double c = 1.0 / Math.sqrt(9 * d);
        while (true) {
            double x, v;
            do {
                x = random.nextGaussian();
                v = 1 + c * x;
            } while (v <= 0);

            v = v * v * v;
            double u = random.nextDouble();
            if (u < 1 - 0.0331 * x * x * x * x) return d * v * scale;
            if (Math.log(u) < 0.5 * x * x + d * (1 - v + Math.log(v))) return d * v * scale;
        }
    }

    /**
     * 计算β分布CDF
     *
     * @param x
     * @param a
     * @param b
     * @return
     */
    public static double betaCDF(double x, double a, double b) {
        double bt = (x == 0 || x == 1) ? 0 : Math.exp(logGamma(a + b) - logGamma(a) - logGamma(b) + a * Math.log(x) + b * Math.log(1 - x));
        if (x < (a + 1) / (a + b + 2)) {
            return bt * betaCF(x, a, b) / a;
        } else {
            return 1 - bt * betaCF(1 - x, b, a) / b;
        }
    }

    /**
     * β不完全函数的连续分数表示
     *
     * @param x
     * @param a
     * @param b
     * @return
     */
    private static double betaCF(double x, double a, double b) {
        int maxIterations = 100;
        double epsilon = 3.0e-7;
        double am = 1, bm = 1, az = 1, qab = a + b, qap = a + 1, qam = a - 1, bz = 1 - qab * x / qap;
        for (int m = 1; m <= maxIterations; m++) {
            int em = m + m;
            double d = m * (b - m) * x / ((qam + em) * (a + em));
            double ap = az + d * am;
            double bp = bz + d * bm;
            d = -(a + m) * (qab + m) * x / ((a + em) * (qap + em));
            double app = ap + d * az;
            double bpp = bp + d * bz;
            double aold = az;
            am = az;
            bm = bz;
            az = app / bpp;
            bz = 1.0;
            if (Math.abs(az - aold) < (epsilon * Math.abs(az))) {
                return az;
            }
        }
        return az;
    }

    /**
     * 计算γ函数的对数
     *
     * @param x
     * @return
     */
    private static double logGamma(double x) {
        double[] coef = {
                76.18009172947146, -86.50532032941677,
                24.01409824083091, -1.231739572450155,
                0.001208650973866179, -0.000005395239384953
        };
        double y = x;
        double tmp = x + 5.5;
        tmp -= (x + 0.5) * Math.log(tmp);
        double ser = 1.000000000190015;
        for (int j = 0; j < coef.length; j++) {
            ser += coef[j] / ++y;
        }
        return -tmp + Math.log(2.5066282746310005 * ser / x);
    }

}
