package jp.sys_link.labyrinth;

/**
 * Created by Watanabe on 2015/11/23.
 */
public class Time {
    private double m_StartTime = 0;

    /**
     * 経過時間の計測を開始します。
     */
    public void start() {
        m_StartTime = 0;
        m_StartTime = System.currentTimeMillis();
    }

    /**
     * 経過時間を取得します。
     * @return 経過時間
     */
    public double getElipseTime() {
        return (System.currentTimeMillis() - m_StartTime) / 1000;
    }
}