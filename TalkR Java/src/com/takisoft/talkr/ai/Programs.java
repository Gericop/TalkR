package com.takisoft.talkr.ai;

/**
 *
 * @author Gericop
 */
public class Programs {

    private String ask;
    private String answer;

    public Programs(String ask, String answer) {
        this.ask = ask;
        this.answer = answer;
    }

    /**
     * @return the ask
     */
    public String getAsk() {
        return ask;
    }

    /**
     * @return the answer
     */
    public String getAnswer() {
        return answer;
    }
}
