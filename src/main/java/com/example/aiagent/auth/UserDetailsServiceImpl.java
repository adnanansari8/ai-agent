package com.example.aiagent.auth;

import com.example.aiagent.model.User;
import com.example.aiagent.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository)
    {
        this.userRepository=userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

       if (userName==null || userName.isBlank() )
       {   throw new UsernameNotFoundException("UserName can not be null or empty");
       }
        return userRepository.findByUsername(userName).orElseThrow(() -> new UsernameNotFoundException("User not found"));

    }
}
