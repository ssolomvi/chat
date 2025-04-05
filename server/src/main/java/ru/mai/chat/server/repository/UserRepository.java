package ru.mai.chat.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.mai.chat.server.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    User findByLogin(String login);

}
