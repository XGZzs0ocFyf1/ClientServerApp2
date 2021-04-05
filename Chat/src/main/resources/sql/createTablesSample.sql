# create database j3t2;
use j3t2;

drop table if exists history;
drop table if exists users;


create table users
(
    id       int primary key not null auto_increment,
    login    varchar(50)     not null unique,
    password varchar(50)     not null,
    nickname varchar(50)     not null unique
);

create table history
(
    user_id int,
    text    varchar(300) not null,
    constraint fk_user
        foreign key (user_id)
            references users (id)
);

INSERT INTO users (login, password, nickname) VALUES ('a', 'b', 'Johny');
INSERT INTO users(login, password, nickname) VALUES ('c', 'd', 'Alex');
INSERT INTO users(login, password, nickname) VALUES ('e', 'f', 'HaiBao');


#INSERT INTO users(login, password, nickname) VALUES('alex', 'qwe', 'jonny')
    # ("a", "b", "Johny"));
# entries.add(new Entry("c", "d", "Alex"));
# entries.add(new Entry("e", "f", "HaiBao"
