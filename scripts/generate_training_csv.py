import csv
import random
from pathlib import Path

random.seed(42)
out = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "assets" / "parking_availability_training.csv"
rows = []
for _ in range(2500):
    pid = f"P{random.randint(1, 50):03d}"
    hour = random.randint(6, 22)
    dow = random.randint(0, 6)
    holiday = 1 if random.random() < 0.05 else 0
    event = 1 if random.random() < 0.12 else 0
    avg_occ = min(
        1.0,
        max(
            0.1,
            random.gauss(0.55, 0.2)
            + (0.15 if hour in (8, 9, 17, 18) else 0)
            + (0.2 if event else 0),
        ),
    )
    avg_dur = max(30, random.gauss(120, 40))
    cancel = min(0.5, max(0.02, random.gauss(0.12, 0.06) + (0.08 if holiday else 0)))
    score = 2.2 - 2.5 * avg_occ - 1.8 * cancel - 0.9 * event - 0.4 * holiday
    if hour in (8, 9, 17, 18):
        score -= 0.5
    if dow in (5, 6):
        score -= 0.3
    prob = 1 / (1 + 2.718 ** (-score))
    avail = 1 if random.random() < prob else 0
    rows.append(
        [
            pid,
            hour,
            dow,
            holiday,
            round(avg_occ, 3),
            round(avg_dur, 1),
            round(cancel, 3),
            event,
            avail,
        ]
    )

out.parent.mkdir(parents=True, exist_ok=True)
with out.open("w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)
    writer.writerow(
        [
            "Parking_ID",
            "Requested_Hour",
            "Day_of_Week",
            "Public_Holiday",
            "Avg_Occupancy",
            "Avg_Parking_Duration",
            "Cancellation_Rate",
            "Event_Nearby",
            "Available",
        ]
    )
    writer.writerows(rows)
print(f"Wrote {len(rows)} rows to {out}")
