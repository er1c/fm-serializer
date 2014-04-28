package fm.serializer;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlTransient;

public class FooJavaBean {
  protected String name;
  protected int number;
  protected FooEnum fooEnum;
  protected List<String> list;
  protected List<String> listWithoutSetter;
  
  protected String ignoredField1;
  @XmlTransient protected String ignoredField2;
//  @XmlTransient public String ignoredField3;
  
  public String getName() {
    return name;
  }
  
  public void setName(String value) {
    this.name = value;
  }
  
  public int getNumber() {
    return number;
  }
  
  public void setNumber(int value) {
    this.number = value;
  }
  
  public FooEnum getFooEnum() {
    return fooEnum;
  }
  
  public void setFooEnum(FooEnum value) {
    this.fooEnum = value;
  }
  
  public List<String> getList() {
    return list;
  }
  
  public void setList(List<String> value) {
    this.list = value;
  }
  
  public List<String> getListWithoutSetter() {
    if (listWithoutSetter == null) {
      listWithoutSetter = new ArrayList<String>();
    }
    return this.listWithoutSetter;
  }

  @Transient
  public String getIgnoredField1() {
    return ignoredField1;
  }
  
  public void setIgnoredField1(String value) {
    this.ignoredField1 = value;
  }
  
  public String getIgnoredField2() {
    return ignoredField2;
  }
  
  public void setIgnoredField2(String value) {
    this.ignoredField2 = value;
  }
  
//  @XmlTransient
//  public String getIgnoredField4() {
//    return "ignoredField4";
//  }
}
