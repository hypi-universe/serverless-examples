async def invoke(request):
    request['example'] = True
    print(request)
    return request