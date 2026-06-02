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
}
