package com.PM.PMService;

import java.lang.reflect.Method;

import org.aspectj.lang.reflect.MethodSignature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.PM.PMService.annotations.PermissionType;
import com.PM.PMService.models.Permission;
import com.PM.PMService.models.Role;
import com.PM.PMService.models.User;


@Aspect
//Mora @Component anotacija inace nece ucitati askept klasu
@Component
@Configurable
public class PermissionAspect {
	
	final static Logger logger = LogManager.getLogger(PermissionAspect.class);

	@Around("execution(* com.PM.PMService.controllers.*.*(..)) && "
			+ "@annotation(com.PM.PMService.annotations.PermissionType)")
	public Object validator(ProceedingJoinPoint jp){
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>)SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		System.out.println("Username je " + SecurityContextHolder.getContext().getAuthentication().getName());
		RestTemplate template = new RestTemplate();
		User u = template.postForObject("http://"+TheUrls.ip+":"+TheUrls.port+TheUrls.dcUser+"/UserByUsername/", username, User.class);

		if(u != null){
			MethodSignature signature = (MethodSignature) jp.getSignature();
		    Method method = signature.getMethod();
		    PermissionType myAnnotation = method.getAnnotation(PermissionType.class);
			boolean weGood = false;
		    for(Role r : u.getAllowed()){
		    	for(Permission p : r.getPermissions()){
		    		if(myAnnotation.value().equals(p.getNaziv())){
		    			weGood = true;
		    			break;
		    		}
		    	}
		    }
		    Object retVal = null;
		    if(weGood){
						try {
							logger.warn("User  " + u.getId()+ " is trying to access method " + SifrarnikMetoda.methods.get(jp.getSignature().getName()));
							retVal = jp.proceed();
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							logger.error("Error executing " + SifrarnikMetoda.methods.get(jp.getSignature().getName()));
							e.printStackTrace();
						}
				    } else {
				    	logger.error("Inadequate permissions for method " + SifrarnikMetoda.methods.get(jp.getSignature().getName()));
				    }
				    return retVal;
				} else {
					logger.error("Failed to access method " + SifrarnikMetoda.methods.get(jp.getSignature().getName()));
					return null;
				}
	}  

}
