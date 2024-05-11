package hellojpa;

import jakarta.persistence.Entity;

@Entity
public class Book extends Item {

    private String author;
    private String isBn;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsBn() {
        return isBn;
    }

    public void setIsBn(String isBn) {
        this.isBn = isBn;
    }
}
