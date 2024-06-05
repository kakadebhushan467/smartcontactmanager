package com.smart.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface UserRepository extends JpaRepository< User,Integer> {
    
	@Query("select u from User u where u.email=:email")
	public  User getUserByUserName(@Param("email") String email);

	public Page<Contact> findContactsbyUser(int id, Pageable pageable);

	public void save(Contact contact);
}
