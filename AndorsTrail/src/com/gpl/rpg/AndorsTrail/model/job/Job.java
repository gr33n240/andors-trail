package com.gpl.rpg.AndorsTrail.model.job;

public abstract class Job {

    /**
     * The first argument passed to the Job subclasses is the jobId (used for Parcelable)
     * and *must* be unique.
     *
     * TODO: Figure a better way of generating a unique jobId.
     */
    public static final Job[] JOBS = {
        new Fighter(  0),
        new Theif(    1),
    };

    public static final String[] NAMES = {
        JOBS[0].name,
        JOBS[1].name,
    };

    public final String name;
    public final int jobId;

    public Job(final String name, final int jobId) {
        this.name  = name;
        this.jobId = jobId;
    }

}
