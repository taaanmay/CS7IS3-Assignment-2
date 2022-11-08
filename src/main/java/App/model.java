package App;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
public class model {

    public String docNo;
    public String date;
    public String title;
    public String content;
    
    public model(String docNo, String date, String title, String content) {
    	this.docNo = docNo;
    	this.date = date;
    	this.title = title;
    	this.content = content;
    }

    

}



