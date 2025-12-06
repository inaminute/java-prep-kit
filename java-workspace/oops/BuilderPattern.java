package oops;

public class BuilderPattern {
	public void initialize(){
        System.out.println("Builder Pattern Initialized");
        demonstrateBuilderPattern();
    }


    private void demonstrateBuilderPattern(){
        System.out.println("=== Builder Pattern Demonstration ===");
        
        User user = new User.Builder("1", "John Doe").build();
        System.out.println("User created with ID: " + user.id + " and Name: " + user.name);
	}
}

class User{
	public String id;
	public String name;
	
	public String email;
	
	private User(Builder builder){
		this.id = builder.id;
		this.name = builder.name;
		this.email = builder.email;
		
	}
	
	public static class Builder{
		private String id;
		private String name;
		
		private String email;
	
		public Builder(String id, String name){
			//Perform validation
			this.id = id;
			this.name = name;
		}
		
		public Builder setEmail(String email){
			this.email = email;
			return this;
		}
		
		public User build(){
			return new User(this);
		}
		
		
	}
}
