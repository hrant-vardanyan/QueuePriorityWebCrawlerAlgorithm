package com.hrant.model;

import java.util.LinkedList;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

/*
 * Database URL entity
 * Author: Hrant Vardanyan
 */
@Entity
public class UrlEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String url;
	private double score;
	private boolean isInter;
	private String parent;
	@Transient
	private LinkedList<UrlEntry> children;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}
	
	

	public void setUrl(String url) {
		this.url = url;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public boolean isInter() {
		return isInter;
	}

	public void setInter(boolean isInter) {
		this.isInter = isInter;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public LinkedList<UrlEntry> getChildren() {
		return children;
	}

	public void setChildren(LinkedList<UrlEntry> children) {
		this.children = children;
	}

}
