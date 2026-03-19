package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.observer.BombObserver;
import edu.hitsz.observer.BombResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 炸弹道具
 * 使用观察者模式，生效时通知所有观察者（敌机和敌机子弹）
 * 
 * @author hitsz
 */
public class BombProp extends AbstractProp {

    /**
     * 观察者列表
     */
    private final List<BombObserver> observers;

    /**
     * 构造函数
     * 
     * @param locationX 初始x坐标
     * @param locationY 初始y坐标
     * @param speedX x方向速度
     * @param speedY y方向速度
     */
    public BombProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
        this.observers = new ArrayList<>();
    }

    /**
     * 添加观察者
     * 
     * @param observer 观察者对象
     */
    public void addObserver(BombObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * 移除观察者
     * 
     * @param observer 观察者对象
     */
    public void removeObserver(BombObserver observer) {
        observers.remove(observer);
    }

    /**
     * 通知所有观察者，炸弹道具生效
     * 每个观察者做出相应的反应（消失、减血等）
     * 
     * @return 返回获得的总分数
     */
    public int notifyObservers() {
        int totalScore = 0;
        System.out.println("=== 炸弹道具生效！通知所有观察者 ===");
        
        // 创建观察者列表的副本，避免在遍历过程中修改原列表
        List<BombObserver> observersCopy = new ArrayList<>(observers);
        
        for (BombObserver observer : observersCopy) {
            BombResult result = observer.reactToBomb();
            totalScore += result.getScoreGained();
            
            // 如果观察者应该消失，则从列表中移除
            if (result.isShouldVanish()) {
                observers.remove(observer);
            }
        }
        
        System.out.println("炸弹道具生效完毕！共获得 " + totalScore + " 分");
        return totalScore;
    }

    /**
     * 炸弹道具生效
     * 注意：此方法只打印消息，实际的通知逻辑由外部调用notifyObservers()完成
     * 这样设计是为了让Game类能够在通知观察者后获取分数
     * 
     * @param heroAircraft 英雄机（暂时未使用）
     */
    @Override
    public void activate(HeroAircraft heroAircraft) {
        System.out.println("BombSupply active!");
    }

    /**
     * 获取观察者数量（用于测试）
     * 
     * @return 观察者数量
     */
    public int getObserverCount() {
        return observers.size();
    }
}