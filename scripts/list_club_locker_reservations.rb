#!/usr/bin/env ruby

require 'json'
require 'uri'
require 'net/http'
require 'date'

BASE_URL = "https://api.ussquash.com"

def usage!
  warn "usage: ruby list_club_locker_reservations.rb YYYY-MM"
  warn "example: ruby list_club_locker_reservations.rb 2025-09"
  warn "(copy the ClubLocker token to clipboard first via the bookmarklet)"
  exit 1
end

month = ARGV[0]

abort "usage: ruby list_club_locker_reservations.rb YYYY-MM" unless month

begin
  Date.strptime(month, "%Y-%m")
rescue ArgumentError
  usage!
end

# --------------------------------------------------
# Resolve month into from_date / to_date
# --------------------------------------------------
month_date = Date.strptime(month, "%Y-%m")
from_date = month_date.strftime("%Y-%m-%d")
to_date   = (month_date.next_month - 1).strftime("%Y-%m-%d")

# --------------------------------------------------
# Read token from clipboard
# --------------------------------------------------
access_token = `pbpaste`.strip
abort "clipboard is empty or does not contain a token" if access_token.empty?
@token_header = "Bearer #{access_token}"

# --------------------------------------------------
# API calls
# --------------------------------------------------
def get_slots_taken(from, to)
  uri = URI("#{BASE_URL}/resources/res/clubs/1413/slots_taken/from/#{from}/to/#{to}")
  req = Net::HTTP::Get.new(uri)
  req['Authorization'] = @token_header

  res = Net::HTTP.start(uri.hostname, uri.port, use_ssl: true) do |http|
    http.request(req)
  end

  raise "request failed: #{res.body}" unless res.is_a?(Net::HTTPSuccess)
  JSON.parse(res.body)
end

def players(reservation_id)
  uri = URI("#{BASE_URL}/resources/res/reservations/#{reservation_id}")
  req = Net::HTTP::Get.new(uri)
  req['Authorization'] = @token_header

  res = Net::HTTP.start(uri.hostname, uri.port, use_ssl: true) do |http|
    http.request(req)
  end

  return [] unless res.is_a?(Net::HTTPSuccess)

  resp_json = JSON.parse(res.body)
  resp_json["players"]
    .select { |p| p["type"] == "member" }
    .map { |p| p["text"] }
end

# --------------------------------------------------
# Processing
# --------------------------------------------------
json = get_slots_taken(from_date, to_date)

@reservations_by_player = {}

def puts_reservation(reservation)
  courts = {
    1411 => "squash",
    1688 => "squash",
    1689 => "squash",
    1690 => "tennis",
    1691 => "racquets"
  }

  court = courts[reservation["court"]]
  return unless court

  reservation_id = reservation["reservationId"]
  players_in_reservation = players(reservation_id)
  return if players_in_reservation.empty?

  time = reservation["startTime"]
  date = reservation["date"][0..9]

  puts "reservation at #{time} on #{date} for #{court} with players: #{players_in_reservation}"

  players_in_reservation.each do |name|
    @reservations_by_player[name] ||= {}
    @reservations_by_player[name][court] ||= 0
    @reservations_by_player[name][court] += 1
  end
end

unique_reservations = json.uniq { |r| r["reservationId"] }

puts "found #{unique_reservations.length} unique reservations"

unique_reservations
  .each_slice(10) do |reservations|
    reservations.map do |reservation|
      Thread.new { puts_reservation(reservation) }
    end.each(&:join)
  end

@reservations_by_player
  .sort_by { |_, counts| -counts.values.sum }
  .each do |name, counts|
    puts "#{name} played: #{counts}"
  end
