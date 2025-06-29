# shortener.pp

# Ensure required packages are installed
package { ['git', 'maven', 'openjdk-17-jdk']:
  ensure => installed,
}

# Create app directory
file { '/opt/shortener':
  ensure => 'directory',
  owner  => 'root',
  group  => 'root',
  mode   => '0755',
}

# Copy JAR from your Downloads (you can modify this to git clone or curl if hosted)
exec { 'copy-shortener-jar':
  command => '/bin/cp /home/aasavari/Downloads/shortener/target/shortener.jar /opt/shortener/shortener.jar',
  creates => '/opt/shortener/shortener.jar',
  require => File['/opt/shortener'],
}

# Create systemd service unit
file { '/etc/systemd/system/shortener.service':
  ensure  => file,
  content => @("EOF"),
  [Unit]
  Description=Spring Boot URL Shortener
  After=network.target

  [Service]
  User=root
  ExecStart=/usr/bin/java -jar /opt/shortener/shortener.jar
  Restart=always

  [Install]
  WantedBy=multi-user.target
  | EOF
  mode    => '0644',
  require => Exec['copy-shortener-jar'],
}

# Reload systemd, enable and start the service
exec { 'reload-systemd':
  command     => '/bin/systemctl daemon-reexec && /bin/systemctl daemon-reload',
  refreshonly => true,
  subscribe   => File['/etc/systemd/system/shortener.service'],
}

service { 'shortener':
  ensure    => running,
  enable    => true,
  require   => Exec['reload-systemd'],
}

