package com.alibou.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.alibou.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TokenRepository extends JpaRepository<Token, Integer> {

  @Query(value = """
      select t from Token t inner join User u\s
      on t.user.id = u.id\s
      where u.id = :id and (t.expired = false or t.revoked = false)\s
      """)
  List<Token> findAllValidTokenByUser(Integer id);
  boolean deleteByExpiredBefore(Date now);
  Optional<Token> findByToken(String token);
}
