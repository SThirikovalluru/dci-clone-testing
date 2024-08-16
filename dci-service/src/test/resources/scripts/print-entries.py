import json
import sys
import pprint

json_data = None
with open(sys.argv[1], 'r') as json_file:
    data = json_file.read()
    json_data = json.loads(data)

pprint.pprint(json_data)