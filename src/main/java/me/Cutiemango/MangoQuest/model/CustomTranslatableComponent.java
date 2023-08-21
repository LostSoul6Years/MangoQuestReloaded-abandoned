package me.Cutiemango.MangoQuest.model;

import net.md_5.bungee.api.chat.BaseComponent;

public class CustomTranslatableComponent extends BaseComponent{
	private String translate;
	public String getTranslate() {
		return translate;
	}
	public void setTranslate(String translate) {
		this.translate = translate;
	}
	public CustomTranslatableComponent(String translate) {
		this.translate = translate;
	}
	public String toString() {
	    return "TranslatableComponent(translate=" + getTranslate() +")";
	  }
	
	  public boolean equals(Object o) {
		    if (o == this)
		      return true; 
		    if (!(o instanceof CustomTranslatableComponent))
		      return false; 
		    CustomTranslatableComponent other = (CustomTranslatableComponent)o;
		    return other.translate.equals(this.translate);
		  }
	  
	@Override
	public BaseComponent duplicate() {
		// TODO Auto-generated method stub
		return new CustomTranslatableComponent(this.translate);
	}
	
}
