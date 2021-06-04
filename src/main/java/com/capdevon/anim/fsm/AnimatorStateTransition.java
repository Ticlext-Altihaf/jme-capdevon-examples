package com.capdevon.anim.fsm;

import java.util.ArrayList;
import java.util.List;

/**
 * Transitions define when and how the state machine switches from one state to another.
 * A transition happens when all its conditions are met.
 *
 * @author capdevon
 */
public class AnimatorStateTransition {

    private AnimatorController animator;

    //The time at which the destination state will start.
    protected float offset = 0;
    //The duration of the transition.
    protected float duration = 0.25f;
    //Determines whether the duration of the transition is reported in a fixed duration in seconds or as a normalized time.
    protected boolean hasFixedDuration = true;
    //If AnimatorStateTransition.hasExitTime is true, exitTime represents the exact time at which the transition can take effect.
    protected float exitTime = 0.75f;
    //When active the transition will have an exit time condition.
    protected boolean hasExitTime = false;
    //Mutes the transition. The transition will never occur.
    protected boolean mute = false;
    //The destination state of the transition.
    protected AnimatorState destinationState;
    //AnimatorCondition conditions that need to be met for a transition to happen.
    protected List<AnimatorCondition> conditions = new ArrayList<>();

    /**
     * Constructor.
     * @param animator
     */
    protected AnimatorStateTransition(AnimatorController animator) {
        this.animator = animator;
    }

    /**
     * Utility function to remove a condition from the transition.
     * @param condition 
     */
    public void removeCondition(AnimatorCondition condition) {
        conditions.remove(condition);
    }

    /**
     * Utility function to add a condition to a transition.
     * @param mode
     * @param threshold
     * @param parameter 
     */
    public void addCondition(AnimatorConditionMode mode, float threshold, String parameter) {
        if (animator.getParameter(parameter) == null) {
            throw new IllegalArgumentException("AnimatorControllerParameter not found: " + parameter);
        }

        AnimatorCondition condition = new AnimatorCondition();
        condition.mode = mode;
        condition.threshold = threshold;
        condition.parameter = parameter;
        conditions.add(condition);
    }

    /**
     * InternalCall.
     * @param sourceState
     * @param layerName
     * @return
     */
    protected boolean checkConditions(AnimatorState sourceState, String layerName) {

        boolean doTransition = true;

        for (AnimatorCondition condition : conditions) {
            if (!condition.evalute(animator.parameters)) {
                doTransition = false;
                break;
            }
        }

        if (doTransition && hasExitTime) {
            double animTime = animator.animComposer.getTime(layerName);
            double animDuration = animator.animComposer.getAction(sourceState.motion.name).getLength();
            double animPercent = animTime / animDuration;
            return animPercent > exitTime;
        }

        return doTransition;
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public float getExitTime() {
        return exitTime;
    }

    public void setExitTime(float exitTime) {
        this.exitTime = exitTime;
    }

    public boolean hasExitTime() {
        return hasExitTime;
    }

    public void setHasExitTime(boolean hasExitTime) {
        this.hasExitTime = hasExitTime;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

}
