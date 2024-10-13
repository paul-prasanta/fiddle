/*
 * Copyright (C) 2024 Prasanta Paul, http://prasanta-paul.blogspot.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.secapp.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

//@Service
public class MyUserDetailsService implements UserDetailsService {

	@Autowired
	UsersRepository usersRepository;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.info("Authenticate user: "+ username);
		
		List<Users> users = usersRepository.findByUserName(username);
		if(users == null || users.size() == 0) {
			logger.info("User not found: "+ username);
			throw new UsernameNotFoundException("UserName "+ username +" doesn't exists");
		}
		
		Users user = users.get(0);
		
		logger.info("User Instance: "+ user);
		
		GrantedAuthority authority = () -> {
			return user.getRole().name();
		};
		
		// For multiple content level access roles, use multiple content permissions for each role
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>(Arrays.asList(authority));
		
		return new MyUser(user, authorities);
	}
	
	// Get currently logged in user's entity instance (for business logic)
	public static Users getLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if(authentication == null || authentication.getPrincipal() == null)
			return null;
		
		return ((MyUser) authentication.getPrincipal()).getUser();
	}
}
