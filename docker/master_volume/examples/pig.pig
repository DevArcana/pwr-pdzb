lines = LOAD '/examples/data.jsonl'  USING JsonLoader('name:chararray, age:int');
DUMP lines;

