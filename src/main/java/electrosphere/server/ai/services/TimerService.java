package electrosphere.server.ai.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import electrosphere.logger.LoggerInterface;

public class TimerService implements AIService {

    /**
     * The map of timer id -> number of frames left on the timer
     */
    Map<Integer,Integer> timerMap = new HashMap<Integer,Integer>();

    /**
     * The iterator for timer ids
     */
    int idIterator = 0;

    /**
     * Lock for ids so we don't overlap
     */
    Semaphore idLock = new Semaphore(1);

    @Override
    public void exec() {
        for(Integer timerId : timerMap.keySet()){
            int currentTime = timerMap.get(timerId);
            int newTime = currentTime - 1;
            if(newTime < 0){
                newTime = 0;
            }
            timerMap.put(timerId,newTime);
        }
    }

    /**
     * Creates a timer
     * @return The id of the timer
     */
    public int createTimer(){
        int id = 0;
        idLock.acquireUninterruptibly();
        id = idIterator++;
        timerMap.put(id,0);
        idLock.release();
        return id;
    }

    /**
     * Creates a timer
     * @param frameCount The number of frames left in the timer
     * @return The id of the timer
     */
    public int createTimer(int frameCount){
        int id = 0;
        idLock.acquireUninterruptibly();
        id = idIterator++;
        timerMap.put(id,frameCount);
        idLock.release();
        return id;
    }

    /**
     * Deletes a timer
     * @param timerId The id of the timer
     */
    public void deleteTimer(int timerId){
        timerMap.remove(timerId);
    }

    /**
     * Checks if the timer is still active
     * @param timerId The id of the timer
     * @return true if still active, false if has completed or has been deleted
     */
    public boolean isActive(int timerId){
        if(!timerMap.containsKey(timerId)){
            return false;
        }
        return timerMap.get(timerId) > 0;
    }

    /**
     * Resets a timer to a given frame count
     * @param timerId The id of the timer
     * @param frameCount The number of frames remaining for the timer
     */
    public void resetTimer(int timerId, int frameCount){
        if(!timerMap.containsKey(timerId)){
            LoggerInterface.loggerAI.ERROR(new IllegalArgumentException("Trying to reset a timer that does not exist!"));
        }
        timerMap.put(timerId,frameCount);
    }

    @Override
    public void shutdown() {
    }
    
}
