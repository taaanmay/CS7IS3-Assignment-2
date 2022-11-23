package app.model.childModel;

import app.model.QueryObject;
import lombok.Data;

/**
 * @author Siuyun Yip
 * @version 1.0
 * @date 2022/11/18 15:52
 */
@Data
public class TopicModel extends QueryObject {

    private String topicNum;

    private String title;

    private String description;

    private String narrative;

    @Override
    public String toString() {
        return description + " " + narrative;
    }
}
