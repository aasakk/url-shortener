# setup.pp

package { 'git':
  ensure => installed,
}

package { 'maven':
  ensure => installed,
}

package { 'openjdk-17-jdk':
  ensure => installed,
}

