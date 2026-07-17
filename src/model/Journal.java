package model;

public class Journal extends LibraryItem {
    private String volume;
    private String doi;

    public Journal(String id, String title, String author, int year, String volume, String doi) {
        super(id, title, author, year);
        this.volume = volume;
        this.doi = doi;
    }

    @Override
    public String getType() {
        return "Journal";
    }

    @Override
    public String getExtraInfo() {
        return "Vol. " + volume + ", DOI: " + doi;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }
}
