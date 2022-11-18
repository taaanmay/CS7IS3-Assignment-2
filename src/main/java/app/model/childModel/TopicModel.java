package app.model.childModel;

import lombok.Data;

/**
 * @author Siuyun Yip
 * @version 1.0
 * @date 2022/11/18 15:52
 */
@Data
public class TopicModel {

    private String description;

    private String narrative;

    @Override
    public String toString() {
        return description + " " + narrative;
    }
}
