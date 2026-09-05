package com.jetlease.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jetlease.model.entity.Testimonial;

public class TestimonialDao {

    public static List<Testimonial> findAll() throws SQLException {
        PreparedStatement ps = Db.getConnection().prepareStatement("SELECT * FROM testimonials");
        ResultSet rs = ps.executeQuery();
        List<Testimonial> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new Testimonial(rs.getString("name"), rs.getString("role"), rs.getString("quote")));
        }
        rs.close();
        ps.close();
        return list;
    }
}
