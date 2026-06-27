package com.mycompany.proton.dao;

import java.sql.SQLException;
import java.util.List;

public interface DAO<T> {

    T getById(int id) throws SQLException;

    List<T> getAll() throws SQLException;

    void insert(T entity) throws SQLException;

    void update(T entity) throws SQLException;

    void delete(int id) throws SQLException;
}