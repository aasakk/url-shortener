exec { 'start-url-shortener-container':
  command => '/usr/bin/docker start url-shortener-container || /usr/bin/docker run -d --name url-shortener-container -p 9096:9096 url-shortener',
  unless  => '/usr/bin/docker ps --filter "name=url-shortener-container" --filter "status=running" | grep url-shortener-container',
  path    => ['/usr/bin', '/bin'],
}
