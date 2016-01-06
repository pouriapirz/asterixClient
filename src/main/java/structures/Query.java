package structures;

public class Query{
	private String name;
	private String body;

	public Query(String name, String body){
		this.name = name;
		this.body = body;
	}

	public String getName(){
		return name;
	}

	public String getBody(){
		return body;
	}
}