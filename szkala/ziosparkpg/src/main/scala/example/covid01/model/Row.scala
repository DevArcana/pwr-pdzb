package covid01.model

case class Row(
    date: String,
    location: String,
    total_cases: String,
    new_cases: String,
    total_deaths: String,
    new_deaths: String,
    new_cases_per_million: String
)