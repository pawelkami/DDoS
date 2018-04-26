import os
from glob import glob

'''
Skrypt konwertujący zebrane teksty treningowe do jednego pliku, w formacie, w którym OpenNLP potrafi klasyfikować
czyli "klasa tekst_bez_znakow_nowej_linii"
'''

# zapisywanie do głównego pliku
def write_to_dataset_file(text):
    with open("dataset.txt", "a") as f:
        f.write(text)


def get_class_name_from_path(path):
    return path[2:path.rfind(os.sep)]


filenames = [y for x in os.walk(".") for y in glob(os.path.join(x[0], '*.txt'))]

for filename in filenames:
    # bierzemy tylko z katalogów z tekstami o określonych klasach np. cycling/ running/
    if filename.count(os.sep) != 2:
        continue

    class_name = get_class_name_from_path(filename)
    with open(filename, 'r') as f:
        data = f.read().replace('\n', ' ')

        write_to_dataset_file(class_name + " " + data + "\n")
