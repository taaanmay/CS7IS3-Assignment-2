package App.Model;


import lombok.*;

@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
public class   BaseModel {
    public String docNo;
    public String date;
    public String title;
    public String content;
    public String headline;


    public BaseModel(String docNo, String date, String title, String content, String headline) {
        this.docNo = docNo;
        this.date = date;
        this.title = title;
        this.content = content;
        this.headline = headline;
    }



}
