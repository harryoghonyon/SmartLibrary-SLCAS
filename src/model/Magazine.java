package model;

public class Magazine extends LibraryItem {
    private int issueNumber;
    private String frequency;

    public Magazine(String id, String title, String author, int year, int issueNumber, String frequency) {
        super(id, title, author, year);
        this.issueNumber = issueNumber;
        this.frequency = frequency;
    }

    @Override
    public String getType() {
        return "Magazine";
    }

    @Override
    public String getExtraInfo() {
        return "Issue #" + issueNumber + ", " + frequency;
    }

    public int getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(int issueNumber) {
        this.issueNumber = issueNumber;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}
