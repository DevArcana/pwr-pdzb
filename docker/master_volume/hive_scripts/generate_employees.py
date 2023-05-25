from random import randrange

f = open("employees.csv", "w")
for i in range(1000):
    f.write(f"{i},Iva Moose,{randrange(99999)},cashier,{randrange(99999)},Phoenix,{randrange(99999)},{randrange(99999)}\n")

f.close()