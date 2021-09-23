package com.example.ocrapplication;

public class LogbookRow
{
    private String date, time, name, temperature, phone, remark;


    public LogbookRow(String date, String time, String name, String temperature, String phone, String remark)
    {
        this.date = date;
        this.time = time;
        this.name = name;
        this.temperature = temperature;
        this.phone = phone;
        this.remark = remark;
    }

    public void setDate()
    {
        this.date = date;
    }

    public String getDate()
    {
        return date;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public String getTime()
    {
        return time;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setTemperature(String temperature)
    {
        this.temperature = temperature;
    }

    public String getTemperature()
    {
        return temperature;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getRemark()
    {
        return remark;
    }
}
