def random = new Random()
def foods = ['piece of bread', 'slab of pork', 'cooked pork chop', 'bite of cake', 'yummy cookie', 'fish', 'golden delicious apple']
[
    'hour change': {
        if (it.hour in [0, 6, 12, 18]) {
            it.world.players.each {
                if (random.nextBoolean()) {
                    if (it.health > 100) it.health -= 10
                    else if (it.health > 50) it.health -= 5
                    else if (it.health > 20) it.health -= 2
                    else if (it.health > 1) it.health -= 1

                    if (it.health < 4) it.sendMessage "May I suggest a ${foods[random.nextInt(foods.size())]} to eat?"
                }
            }
        }
    }
]
