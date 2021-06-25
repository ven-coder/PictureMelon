package luoyi.android.com.wsfzzs.sql.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class SBDataStatistics {
    @Id
    private Long id;
    //期数
    private String periods = "";
    //开奖号码
    private String lotteryNumber = "";
    //开奖时间
    private long lotteryTime = 0;


    @Generated(hash = 129500900)
    public SBDataStatistics(Long id, String periods, String lotteryNumber,
            long lotteryTime) {
        this.id = id;
        this.periods = periods;
        this.lotteryNumber = lotteryNumber;
        this.lotteryTime = lotteryTime;
    }

    @Generated(hash = 430125245)
    public SBDataStatistics() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPeriods() {
        return this.periods;
    }

    public void setPeriods(String periods) {
        this.periods = periods;
    }

    public String getLotteryNumber() {
        return this.lotteryNumber;
    }

    public void setLotteryNumber(String lotteryNumber) {
        this.lotteryNumber = lotteryNumber;
    }

    public long getLotteryTime() {
        return this.lotteryTime;
    }

    public void setLotteryTime(long lotteryTime) {
        this.lotteryTime = lotteryTime;
    }
}
