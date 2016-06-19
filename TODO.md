# Make File/Dir Resource
Move operations on File/Path into FileResource

# Find in html similar to BeautifulSoup

# Turn Context into Encapsulated Context Pattern
- move state out of Context 
- move log factory into Context
- pass Context to method, not to constructor

# Create common usecase
The common usecase has:
- download initial file
- parse initial file
- download chapter files
- parse chapter files
- download page files
- parse page files
- zip chapters

What left for specific usecase:
- get chapter links from initial file
- get page links from page files
- get image links from page files or chapter files

# Remove all checked-exceptions
Don't want to both checked and unchecked exceptions.
One has to go and it cannot be the unchecked one.

Don't know for sure if this is a good idea but have to try anyway.
Also, Closure/Python seems to work ok without checked exceptions.

# Hand-built html parser
Produce an Html document for this app only.

Specifically, need to find elements with nicer syntax:
html.find(
    select attr(class, changePage) as(select)
        option oneOrMore(attr(value)) as(options)
).forEach(e ->
    e.get("options").map(List::cast).forEach(opt ->
        link = html.absUrl(opt.text)
        ...
    )
)

Hmm, for shallow tree this looks for complex than it should.
Just need to do the simpler thing for shallow tree:

for select in html.findAll(select, attr(class, changePage)):
    for option in select.findAt(option, attr(value)):
        ... have both select and option
    

