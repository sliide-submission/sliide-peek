package com.sliide.useractivity.test

internal object Fixtures {
    const val USERS_PAGE_1 = """
        [
          {
            "id": 8484086,
            "name": "Bhargava Adiga",
            "email": "adiga_bhargava@klocko.test",
            "gender": "male",
            "status": "active"
          },
          {
            "id": 8484087,
            "name": "Priya Nair",
            "email": "priya.nair@example.test",
            "gender": "female",
            "status": "inactive"
          }
        ]
    """

    const val USER_DETAIL = """
        {
          "id": 8484086,
          "name": "Bhargava Adiga",
          "email": "adiga_bhargava@klocko.test",
          "gender": "male",
          "status": "active"
        }
    """

    const val USER_POSTS = """
        [
          {
            "id": 282051,
            "user_id": 8484086,
            "title": "Defleo vito cribro hic aggero.",
            "body": "Voluptatem validus corona. Dicta adstringo censura."
          }
        ]
    """

    const val POST_DETAIL = """
        {
          "id": 282051,
          "user_id": 8484086,
          "title": "Defleo vito cribro hic aggero.",
          "body": "Voluptatem validus corona. Dicta adstringo censura."
        }
    """

    const val POST_COMMENTS = """
        [
          {
            "id": 189787,
            "post_id": 282051,
            "name": "Amb. Harit Nehru",
            "email": "nehru_amb_harit@lockman.test",
            "body": "Eligendi itaque consequatur. Unde sunt voluptatem."
          }
        ]
    """

    const val USER_TODOS = """
        [
          {
            "id": 104820,
            "user_id": 8484086,
            "title": "Illo adulescens cuius curtus acies thermae.",
            "due_on": "2026-06-16T00:00:00.000+05:30",
            "status": "pending"
          }
        ]
    """
}
