# Make File/Dir Resource
Move operations on File/Path into FileResource

# Make IO abstraction
Core operations are reading and writing.
Can have two abtractions: InputSource and OutputSource.
Additional concerns (read/write texts, buffering, filtering, parsing, ect.) can be achieved with Decorators.

# Remove all checked-exceptions
Don't want to both checked and unchecked exceptions.
One has to go and it cannot be the unchecked one.

Don't know for sure if this is a good idea but have to try anyway.
Also, Closure/Python seems to work ok without checked exceptions.

# Find in html similar to BeautifulSoup

# Hand-built html parser
Just need to produce a nested map for html.
Use JSoup for references.

# Make general TreeBuilder
Having TreeBuilder as base to build arbitrary structures is too complex.
Let's just build a general trees then we can convert them to anything else.

General tree:
    tree = root context?
    root = node
    context = node
    node = name (node)*
    name = immutable-value

=> can use Entry for node-type, entry-key can be name and entry-value can be child-nodes.
    Entry = Key Value
    Value = Name | Iterable<Entry>