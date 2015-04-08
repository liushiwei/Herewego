
package com.hhsir.herewego.igs;

public class Player {

    private String name;
    private String rank;//等级
    private String language;
    private String country;
    private String info;//棋手的信息
    private String wl;//获胜的对局数/失败的对局数
    private int Obs;//正在观看的对局
    private int pl;//正在下的对局的编号
    private String idle;//空闲时间
    /*
     * 标志包括: Q, S, X 和 !
      Q = toggle quiet on (不看系统消息)
      S = toggle shout off (不看叫喊信息, 系统管理员的除外)
      X = toggle open off (棋手不接受对局请求)
      ! = toggle looking on (棋手正急于寻求对局)
     */
    private String flag;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

}
