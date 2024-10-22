/*
 * Student ID: 1394392
 * Name: Renfei Yu (Klaus)
 */

public class MatchStatus {
    private String username;
    private String status;
    public MatchStatus(String playerName) {
        this.username=playerName;
    }

    public void setStatus(String temp){
        this.status = temp;
    }

    public String getStatus(){
        return this.status;
    }
    
    public String getUsername(){
        return username;
    }
}
