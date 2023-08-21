package me.Cutiemango.MangoQuest.versions.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;

import me.Cutiemango.MangoQuest.DebugHandler;

public class StupidReflection {
   public static Optional<Object> getField(Object caller,String fieldname){
	  try {
		  Class<?> callerclass = caller.getClass();
		   Field f = callerclass.getDeclaredField(fieldname);
		   f.setAccessible(true);
		   return Optional.ofNullable(f.get(caller));
	  }catch(Exception e) {
		  e.printStackTrace();
	  }
	  return Optional.empty();
   }
   
   public static Optional<Object> callMethodWithObjectArgs(Object caller,String methodName, Object... callee){
	   try {
		   
		   Class<?> callerClass = caller == null ? null : caller.getClass();
		   List<Class<?>> callees = new ArrayList<>();
		   Class<?>[] calleetypes = null;
		   
		   boolean isEmpty = true;
		   boolean isGeneric = false;
		   if(callee.length != 0 && callee != null) {
			   calleetypes = new Class<?>[callee.length];;
			   
			   for(int i = 0; i < callee.length;i++) {
				   if(callee[i] != null) {

					   //isGeneric = StupidReflection.isGeneric(callee[i].getClass());
					   calleetypes[i] = getSafeType(callee[i]);
					   //DebugHandler.log(2, "lmaotype: "+callee[i].getClass().toGenericString());
					   
				   }
				   
				   
				   
				   //Bukkit.getLogger().info("Parameter type: "+calleetypes[i].toGenericString());
				   
				   isEmpty = false;
			   }
		   }
		   
		   Method toCall = null;
		   
		   
		   
		   if(true ) {
			   
			   for(Method m:callerClass.getDeclaredMethods()) {
				   if(m.getName().equals(methodName)) {
					   if(m.getParameterCount() != callee.length) {
						   ////Bukkit.getLogger().info("Oh no! Rejected "+m.toGenericString()+" for parameter count not matching");
						   continue;
					   }
					   boolean error = false;
					   for(int i = 0; i < m.getParameterCount();i++) {
						   Parameter param = m.getParameters()[i];
						   
						   boolean match = param.getType().isAssignableFrom(calleetypes[i]) || param.getType().isInstance(callee[i]);
						   //Bukkit.getLogger().info(String.format("Arg %d: Is %s Assignable from %s: %s", i, param.getType().toGenericString(),callee[i].getClass().toGenericString(), match));
						   
						   if(calleetypes[i] == null || !match) {
							   error = true;
							   break;
						   }
						   
						   
					   }
					   
					   if(error) {
						   continue;
					   }
					   /*for(int i = 0; i < m.getParameterCount();i++) {
							
						      Parameter param = m.getParameters()[i];
						      
						      
						      
						      if(param.getParameterizedType()!=null && param.getParameterizedType() instanceof ParameterizedType) {
						    	  ParameterizedType typebro = (ParameterizedType) param.getParameterizedType();
						    	  if(typebro.getActualTypeArguments().length <=0) {
						    		  continue;
						    	  }
						    	  if(!isGeneric(callee[i].getClass())) {
						    		  error=true;
						    		  break;
						    	  }
						    	  for(int jk =0; jk < typebro.getActualTypeArguments().length;jk++) {
						    		 if(typebro.getActualTypeArguments()[jk].getClass().isAssignableFrom(((ParameterizedType)callee[i].getClass().getGenericSuperclass()).getActualTypeArguments()[jk].getClass())) {
						    			 continue;
						    		 }
						    		 error=true;
						    		 for(Type interfaces:callee[i].getClass().getGenericInterfaces()) {
						    			 for(int jk1=0; jk < ((ParameterizedType)interfaces).getActualTypeArguments().length;jk1++) {
						    				if(!typebro.getActualTypeArguments()[jk].getClass().isAssignableFrom(((ParameterizedType)interfaces).getActualTypeArguments()[jk1].getClass())) {
						    					error=true;
						    					break;
						    				}
						    				error = false;
						    			 }
						    			 
						    			 if(error) {
						    				 break;
						    			 }
						    			 
						    		 }
						    		 if(error) {
						    			 break;
						    		 }
						    	  }
						      }
						      
						      if(error) {
						    	  break;
						      }
						      
						   
							  if(!param.getClass().isAssignableFrom(callee[i].getClass())) {
								  error = true; break; 
							  }
							 
						   
						   
						   
					   }
					   if(error) {
						   //Bukkit.getLogger().info("Oh no! Rejected "+m.toGenericString()+" for class not match:");
						   //Bukkit.getLogger().info(callee[0].getClass().toGenericString());
						   //Bukkit.getLogger().info(m.toGenericString());
						   continue;
					   }
					   toCall = m;
				   }*/
				   toCall = m;
				   break;
			   }
			 }
			   
		   }
		   
		   
		   
		   
		  
		   
		  if(toCall != null) {
			  toCall.setAccessible(true);
			 // DebugHandler.log(2, "called method: "+toCall.toGenericString());
			  //Bukkit.getLogger().info("FUCKING CALLING: "+toCall.toGenericString());
			  return Optional.ofNullable(toCall.invoke(caller, callee));
		  }else {
			  DebugHandler.log(2, methodName+" for "+caller.getClass().toGenericString()+" is FUCKING NULL LIKE WTF");
			  return Optional.empty();
		  }
		   
	   }catch(Exception e) {
		   e.printStackTrace();
	   }
	   return Optional.empty();
   }
   /*
   public static Optional<Object> callMethodWithObjectArgsSafe(Object caller,String methodName, Object... callee){
	   try {
		   
		   Class<?> callerClass = caller == null ? null : caller.getClass();
		   List<Class<?>> callees = new ArrayList<>();
		   Class<?>[] calleetypes = null;
		   
		   boolean isEmpty = true;
		   boolean isGeneric = false;
		   if(callee.length != 0 && callee != null) {
			   calleetypes = new Class<?>[callee.length];;
			   
			   for(int i = 0; i < callee.length;i++) {
				   if(callee[i] != null) {

					   //isGeneric = StupidReflection.isGeneric(callee[i].getClass());
					   calleetypes[i] = getSafeType(callee[i]);
					   //DebugHandler.log(2, "lmaotype: "+callee[i].getClass().toGenericString());
					   
				   }
				   
				   
				   
				   //Bukkit.getLogger().info("Parameter type: "+calleetypes[i].toGenericString());
				   
				   isEmpty = false;
			   }
		   }
		   
		   Method toCall = null;
		   
		   
		   
		   if(isGeneric || true) {
			   
			   for(Method m:callerClass.getDeclaredMethods()) {
				   if(m.getName().equals(methodName)) {
					   if(m.getParameterCount() != callee.length) {
						   ////Bukkit.getLogger().info("Oh no! Rejected "+m.toGenericString()+" for parameter count not matching");
						   continue;
					   }
					   boolean error = false;
					   for(int i = 0; i < m.getParameterCount();i++) {
						   Parameter param = m.getParameters()[i];
						   
						   boolean match = param.getType().isAssignableFrom(calleetypes[i]) || param.getType().isInstance(callee[i]);
						   Bukkit.getLogger().info(String.format("Arg %d: Is %s Assignable from %s: %s", i, param.getType().toGenericString(),callee[i].getClass().toGenericString(), match));
						   
						   if(calleetypes[i] == null || !match) {
							   error = true;
							   break;
						   }
						   
						   
					   }
					   
					   if(error) {
						   continue;
					   }
					   /*for(int i = 0; i < m.getParameterCount();i++) {
							
						      Parameter param = m.getParameters()[i];
						      
						      
						      
						      if(param.getParameterizedType()!=null && param.getParameterizedType() instanceof ParameterizedType) {
						    	  ParameterizedType typebro = (ParameterizedType) param.getParameterizedType();
						    	  if(typebro.getActualTypeArguments().length <=0) {
						    		  continue;
						    	  }
						    	  if(!isGeneric(callee[i].getClass())) {
						    		  error=true;
						    		  break;
						    	  }
						    	  for(int jk =0; jk < typebro.getActualTypeArguments().length;jk++) {
						    		 if(typebro.getActualTypeArguments()[jk].getClass().isAssignableFrom(((ParameterizedType)callee[i].getClass().getGenericSuperclass()).getActualTypeArguments()[jk].getClass())) {
						    			 continue;
						    		 }
						    		 error=true;
						    		 for(Type interfaces:callee[i].getClass().getGenericInterfaces()) {
						    			 for(int jk1=0; jk < ((ParameterizedType)interfaces).getActualTypeArguments().length;jk1++) {
						    				if(!typebro.getActualTypeArguments()[jk].getClass().isAssignableFrom(((ParameterizedType)interfaces).getActualTypeArguments()[jk1].getClass())) {
						    					error=true;
						    					break;
						    				}
						    				error = false;
						    			 }
						    			 
						    			 if(error) {
						    				 break;
						    			 }
						    			 
						    		 }
						    		 if(error) {
						    			 break;
						    		 }
						    	  }
						      }
						      
						      if(error) {
						    	  break;
						      }
						      
						   
							  if(!param.getClass().isAssignableFrom(callee[i].getClass())) {
								  error = true; break; 
							  }
							 
						   
						   
						   
					   }
					   if(error) {
						   //Bukkit.getLogger().info("Oh no! Rejected "+m.toGenericString()+" for class not match:");
						   //Bukkit.getLogger().info(callee[0].getClass().toGenericString());
						   //Bukkit.getLogger().info(m.toGenericString());
						   continue;
					   }
					   toCall = m;
				   }
				   toCall = m;
				   break;
			   } // add the bulk end mark here if dereferenced
			 }
			   
		   }else {
			   toCall = callerClass.getDeclaredMethod(methodName,isEmpty ? null : calleetypes);
		   }
		   
		   
		   
		   
		  
		   
		  if(toCall != null) {
			  toCall.setAccessible(true);
			  DebugHandler.log(2, "called method: "+toCall.toGenericString());
			  //Bukkit.getLogger().info("FUCKING CALLING: "+toCall.toGenericString());
			  return Optional.ofNullable(toCall.invoke(caller, callee));
		  }else {
			  DebugHandler.log(2, methodName+" for "+caller.getClass().toGenericString()+" is FUCKING NULL LIKE WTF");
			  return Optional.empty();
		  }
		   
	   }catch(Exception e) {
		   e.printStackTrace();
	   }
	   return Optional.empty();
   }*/
   
   public static boolean isGeneric(Class<?> c)
   {
       boolean hasTypeParameters = hasTypeParameters(c);
       boolean hasGenericSuperclass = hasGenericSuperclass(c);
//     boolean hasGenericSuperinterface = hasGenericSuperinterface(c);
//     boolean isGeneric = hasTypeParameters || hasGenericSuperclass || hasGenericSuperinterface;
       boolean isGeneric = hasTypeParameters || hasGenericSuperclass;

       //System.out.println(c.getName() + " isGeneric: " + isGeneric);

       return isGeneric;
   }

   public static boolean hasTypeParameters(Class<?> c)
   {
       boolean flag = c.getTypeParameters().length > 0;
       //System.out.println(c.getName() + " hasTypeParameters: " + c.getTypeParameters().length);
       return flag;
   }

   public static boolean hasGenericSuperclass(Class<?> c)
   {
       Class<?> testClass = c;

       while (testClass != null)
       {
           Type t = testClass.getGenericSuperclass();

           if (t instanceof ParameterizedType)
           {
               //System.out.println(c.getName() + " hasGenericSuperclass: " + t.getClass().getName());
               return true;
           }

           testClass = testClass.getSuperclass();
       }

       return false;
   }

   public static boolean hasGenericSuperinterface(Class<?> c)
   {
       for (Type t : c.getGenericInterfaces())
       {
           if (t instanceof ParameterizedType)
           {
               //System.out.println(c.getName() + " hasGenericSuperinterface: " + t.getClass().getName());
               return true;
           }
       }

       return false;
   }
   
   public static Class<?> getSafeType(Object obj){
	
	   if(obj.getClass() == Integer.class) {
		   return int.class;
		   
	   }else if(obj.getClass() == Byte.class) {
		   return byte.class;
	   }else if(obj.getClass() == Short.class) {
		   return short.class;
	   }else if(obj.getClass() == Long.class) {
		   return long.class;
	   }else if(obj.getClass() == Float.class) {
		   return float.class;
	   }else if(obj.getClass() == Double.class) {
		   return double.class;
	   }else if(obj.getClass() == Boolean.class) {
		   
		   return boolean.class;
		   
	   }else if(obj.getClass() == Character.class) {
		   return char.class;
	   }else {
		   return obj.getClass();
	   }
	   
	   
	   
	   
   }

}
