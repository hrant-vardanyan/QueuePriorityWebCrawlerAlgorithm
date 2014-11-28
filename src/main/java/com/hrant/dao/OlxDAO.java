package com.hrant.dao;


import javax.persistence.EntityManager;


import com.hrant.JpaUtil;
import com.hrant.model.UrlEntry;

/*
 * Database connection handler
 * Author: Hrant Vardanyan
 */
public class OlxDAO {

	/*
	 * Singletone pattern
	 */
	private static OlxDAO INSTANCE = new OlxDAO();

	public static OlxDAO getInstance() {
		return OlxDAO.INSTANCE;
	}

	private OlxDAO() {
	}

	/*
	 * Entry Saver
	 */
	public void addMessage(UrlEntry urlEntry) {
		EntityManager entityManager = null;
		try {
			// Open connection and save entuty
			entityManager = JpaUtil.getEMF().createEntityManager();
			entityManager.getTransaction().begin();
			entityManager.persist(urlEntry);
			entityManager.getTransaction().commit();
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
	}

}
