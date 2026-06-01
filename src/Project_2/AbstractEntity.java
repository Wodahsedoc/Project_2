
package Project_2;

/**
 *
 * @author aneirinblosch
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public abstract class AbstractEntity {

    private String id;
    private final String createdDate;


    public AbstractEntity() {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.createdDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }


    public AbstractEntity(String id, String createdDate) {
        this.id = id;
        this.createdDate = createdDate;
    }


    public String getId() {
        return id;
    }

    public String getCreatedDate() {
        return createdDate;
    }


    public void setId(String id) {
        this.id = id;
    }


    public abstract void displayDetails();


    @Override
    public String toString() {
        return "ID: " + id + " | Created: " + createdDate;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbstractEntity other = (AbstractEntity) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}