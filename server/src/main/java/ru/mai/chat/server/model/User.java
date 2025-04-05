package ru.mai.chat.server.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "user")
public class User extends AbstractAggregateRoot<User> implements Persistable<Long> {

    @Id
    private Long id;
    private String login;
    private String diffieHellmanNumber;

//    @Override
//    public Long getId() {
//        return id;
//    }

    public User() {

    }

    @PersistenceConstructor
    public User(Long id, String login, String diffieHellmanNumber) {
        this.id = id;
        this.login = login;
        this.diffieHellmanNumber = diffieHellmanNumber;
    }

    @Override
    @Transient
    public boolean isNew() {
        return id == null;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getDiffieHellmanNumber() {
        return diffieHellmanNumber;
    }

    public void setDiffieHellmanNumber(String diffieHellmanNumber) {
        this.diffieHellmanNumber = diffieHellmanNumber;
    }

}
